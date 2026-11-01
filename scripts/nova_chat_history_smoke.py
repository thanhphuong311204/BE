"""Live MySQL/Gemini/JWT history verification using disposable test accounts.

No API key, JWT, password or conversation text is printed. Only accounts created
by this invocation are removed in finally. Existing users and chats are untouched.
"""
import base64
import hashlib
import hmac
import json
import secrets
import time
from pathlib import Path

import requests
from style_library import connect, settings

BASE = "http://localhost:8080/api"


def main():
    accounts = []
    result = {}

    def expect(response, code):
        if response.status_code != code:
            raise RuntimeError("Unexpected HTTP status: " + str(response.status_code))
        return response.json() if response.content else None

    def account():
        email = "nova-history-smoke-" + str(time.time_ns()) + "@example.invalid"
        password = secrets.token_urlsafe(24)
        data = expect(requests.post(BASE + "/auth/register", json={
            "fullName": "NOVA history smoke test", "email": email,
            "phoneNumber": "09" + str(secrets.randbelow(100000000)).zfill(8),
            "password": password, "confirmPassword": password,
        }, timeout=15), 201)
        accounts.append((data["id"], email))
        logged = expect(requests.post(BASE + "/auth/login",
            json={"identifier": email, "password": password}, timeout=15), 200)
        return data["id"], email, password, {"Authorization": "Bearer " + logged["token"]}

    def send(headers, payload):
        for attempt in range(3):
            response = requests.post(BASE + "/ai/chat", headers=headers, json=payload, timeout=22)
            if response.status_code == 200:
                return response.json()
            if response.status_code not in (502, 503, 504):
                break
            if attempt < 2:
                time.sleep(1)
        raise RuntimeError("Gemini live call did not succeed; HTTP " + str(response.status_code))

    try:
        user, email, password, headers = account()
        _, _, _, other = account()
        first = send(headers, {"message": "Gợi ý outfit nữ đi học màu kem. Đặt tên outfit là Lá Trà 748; trả lời ngắn."})
        cid = first["conversationId"]
        assert first["messageId"] > first["userMessageId"]
        with connect() as db:
            with db.cursor() as cur:
                cur.execute("SELECT COUNT(*) n FROM chat_messages WHERE conversation_id=%s", (cid,))
                assert cur.fetchone()["n"] == 2
        result["persistedFirstPairInMySql"] = True
        expect(requests.get(BASE + f"/ai/conversations/{cid}/messages", headers=other, timeout=10), 404)
        expect(requests.delete(BASE + f"/ai/conversations/{cid}", headers=other, timeout=10), 404)
        expect(requests.post(BASE + "/ai/chat", headers=other,
            json={"message": "Access denied", "conversationId": cid}, timeout=10), 404)
        assert expect(requests.get(BASE + "/ai/conversations", headers=other, timeout=10), 200)["items"] == []
        result["crossUserReadContinueDeleteBlocked"] = True
        # New login yields a new authenticated session; history must still exist.
        login = expect(requests.post(BASE + "/auth/login",
            json={"identifier": email, "password": password}, timeout=15), 200)
        headers = {"Authorization": "Bearer " + login["token"]}
        listing = expect(requests.get(BASE + "/ai/conversations", headers=headers, timeout=10), 200)
        assert listing["items"][0]["id"] == cid
        second = send(headers, {"message": "Nhắc lại tên outfit mình đặt lúc đầu và gợi ý một phụ kiện.",
            "conversationId": cid})
        assert second["conversationId"] == cid
        assert "748" in second["reply"], "Model did not recall the persisted conversation context"
        messages = expect(requests.get(BASE + f"/ai/conversations/{cid}/messages?size=2",
            headers=headers, timeout=10), 200)
        assert len(messages["items"]) == 2 and messages["hasMore"]
        older = expect(requests.get(BASE + f"/ai/conversations/{cid}/messages",
            params={"size": 2, "beforeId": messages["nextBeforeId"]}, headers=headers, timeout=10), 200)
        assert older["items"][0]["id"] == first["userMessageId"]
        assert older["items"][-1]["id"] < messages["items"][0]["id"]
        result.update({"model": second["model"], "continuedSameConversation": True,
            "recalledPersistedContext": True, "paginationVerified": True, "reloginHistoryVerified": True})
        for path in ("/ai/conversations", f"/ai/conversations/{cid}/messages"):
            expect(requests.get(BASE + path, timeout=10), 401)
        def b64(value):
            return base64.urlsafe_b64encode(value).rstrip(b"=")
        expired = b64(b'{"alg":"HS256","typ":"JWT"}') + b"." + b64(json.dumps({
            "iss": "fashion-api", "sub": str(user), "iat": int(time.time()) - 600,
            "exp": int(time.time()) - 300}).encode())
        signed = expired + b"." + b64(hmac.new(settings()["JWT_SECRET"].encode(), expired, hashlib.sha256).digest())
        expect(requests.get(BASE + "/ai/conversations",
            headers={"Authorization": "Bearer " + signed.decode()}, timeout=10), 401)
        result["missingAndExpiredJwtRejected"] = True
        expect(requests.delete(BASE + f"/ai/conversations/{cid}", headers=headers, timeout=10), 204)
        expect(requests.get(BASE + f"/ai/conversations/{cid}/messages", headers=headers, timeout=10), 404)
        with connect() as db:
            with db.cursor() as cur:
                cur.execute("SELECT COUNT(*) n FROM chat_messages WHERE conversation_id=%s", (cid,))
                assert cur.fetchone()["n"] == 0
        result["deleteCascadeVerified"] = True
        result["passed"] = True
    except Exception as ex:
        result.update({"passed": False, "errorType": type(ex).__name__})
        # Exception text deliberately omitted: HTTP libraries can include credentials.
    finally:
        with connect() as db:
            with db.cursor() as cur:
                for user, email in accounts:
                    cur.execute("DELETE FROM users WHERE id=%s AND email=%s", (user, email))
            db.commit()
        result["testAccountsCleaned"] = len(accounts)
        output = Path(__file__).resolve().parents[1] / "output/nova-chat-history-live.json"
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(json.dumps(result, indent=2), encoding="utf-8")
        print(json.dumps(result))
    return 0 if result.get("passed") else 1


if __name__ == "__main__":
    raise SystemExit(main())
