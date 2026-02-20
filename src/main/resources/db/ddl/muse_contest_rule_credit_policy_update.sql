-- Update existing contest rules to credit-based submission policy.
-- Safe to re-run: UPDATE will only affect matching old phrases.

USE MUSE;

UPDATE contest_rule
SET rule_text = '해당 콘테스트 출품권 1개당 1회 출품 가능 (보유 시 횟수 제한 없음, 콘테스트 간 공유 불가)'
WHERE rule_text IN (
    '1인 1작품만 제출 가능',
    '1인 1작품 제출 가능',
    '1인 2작품까지 제출 가능',
    '해당 콘테스트 출품권 1개당 1회 출품 가능 (보유 시 횟수 제한 없음)'
);
