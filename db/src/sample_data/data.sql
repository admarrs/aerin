
\echo # filling table data.user (2)
COPY data.user (id,name,email,"password") FROM STDIN (FREEZE ON);
1	alice	alice@email.com	pass
2	bob	bob@email.com	pass
\.

analyze user;