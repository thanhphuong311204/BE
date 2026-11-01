-- Additive: legacy text rows remain valid; structured blocks commit with the assistant message.
ALTER TABLE chat_messages ADD COLUMN content_blocks JSON NULL;
