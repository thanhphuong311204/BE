"""Live chat verification. JWT stays in memory; a chat created by this run is cleaned up."""
import base64
import hashlib
import hmac
import json
import time
from pathlib import Path

import requests
from style_library import connect, settings


def b64(value):
    return base64.urlsafe_b64encode(value).rstrip(b"=")


def main():
    cfg = settings()
    with connect() as db:
        with db.cursor() as cur:
            cur.execute("SELECT id FROM users WHERE status='ACTIVE' AND deleted_at IS NULL LIMIT 1")
            user = cur.fetchone()
            cur.execute("SELECT COUNT(*) n FROM styles WHERE is_active=TRUE AND is_explore_enabled=TRUE")
            count = cur.fetchone()["n"]
    if not user:
        raise SystemExit("No active test account available.")
    now = int(time.time())
    signed = b64(b'{"alg":"HS256","typ":"JWT"}') + b"." + b64(json.dumps({
        "iss": "fashion-api", "sub": str(user["id"]), "iat": now, "exp": now + 300
    }).encode())
    token = (signed + b"." + b64(hmac.new(
        cfg["JWT_SECRET"].encode(), signed, hashlib.sha256
    ).digest())).decode()
    url = "http://localhost:8080/api/ai/chat"
    result = {"mysqlActiveStyles": count}
    # No automatic provider retry in production; this smoke test retries transient errors.
    for attempt in range(3):
        try:
            response = requests.post(url, headers={"Authorization": "Bearer " + token},
                json={"message": "Gợi ý phối đồ nữ đi học với màu kem, trả lời ngắn gọn."}, timeout=22)
            body = response.json()
            result.update({"httpStatus": response.status_code, "attempts": attempt + 1})
            if response.ok:
                result.update({"model": body.get("model"), "reply": body.get("reply")})
                break
            result["errorCode"] = body.get("code")
            if response.status_code not in (502, 503, 504):
                break
        except (requests.RequestException, ValueError) as ex:
            result["errorType"] = type(ex).__name__
        if attempt < 2:
            time.sleep(1)
    result["unauthenticatedStatus"] = requests.post(
        url, json={"message": "hello"}, timeout=10).status_code
    if result.get("httpStatus") == 200:
        conversation_id = body.get("conversationId")
        if conversation_id is not None:
            cleanup = requests.delete(
                "http://localhost:8080/api/ai/conversations/" + str(conversation_id),
                headers={"Authorization": "Bearer " + token}, timeout=10)
            result["testConversationCleaned"] = cleanup.status_code == 204
    path = Path(__file__).resolve().parents[1] / "output/nova-chat-live.json"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({k: v for k, v in result.items() if k != "reply"}, ensure_ascii=True))
    if result.get("httpStatus") != 200 or not result.get("reply"):
        raise SystemExit(1)


if __name__ == "__main__":
    main()
