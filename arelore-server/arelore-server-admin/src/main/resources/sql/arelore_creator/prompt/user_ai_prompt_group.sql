USE arelore_creator;

-- ============================================================================
-- 【组维度】AI 提示词（暂不实现功能，仅设计注释，勿执行下方建表语句）
-- 类比 MQ：同步组 ≈ Topic 空间；成员 role ≈ 生产组 / 消费组
--
-- 表规划：
-- 1) user_ai_prompt_group
--    - owner_user_id / name / invite_code / status(NORMAL|DELETED) / ext_info
-- 2) user_ai_prompt_group_member
--    - group_id / user_id / role(PRODUCER|CONSUMER|BOTH) / status(NORMAL|DELETED)
-- 3) user_ai_prompt（组版，与个人表分离或加 group_id；实现时再定）
--    - group_id 归属组；user_id 为创建人；prompt_uuid 组内唯一覆盖导入
-- 4) 标签：实现时可用组字典或沿用个人 tags JSON
--
-- 权限：读 CONSUMER/BOTH；写 PRODUCER/BOTH
-- 一对一兼容：每人一个仅自己 BOTH 的默认组
-- ============================================================================

/*
CREATE TABLE `user_ai_prompt_group` ( ... );
CREATE TABLE `user_ai_prompt_group_member` ( ... );
-- 组版提示词表结构待实现时补充
*/
