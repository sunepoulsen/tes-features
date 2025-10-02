SELECT *
FROM features fe;

SELECT COUNT(*)
FROM features fe;

SELECT fe.*
FROM features fe
WHERE lower(fe.feature_key)=lower('some-key');

SELECT fe.*
FROM features fe
WHERE fe.feature_group_id = 17;
