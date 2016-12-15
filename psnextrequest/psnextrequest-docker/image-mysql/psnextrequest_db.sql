CREATE DATABASE IF NOT EXISTS psnextrequest_db;
USE psnextrequest_db;
CREATE TABLE psnextrequest_db.PSNEXT_PROJECT_TEAM
(
  PROJECT_TEAM_ID BIGINT(20) NOT NULL AUTO_INCREMENT
, PROJECT_TEAM_NAME NVARCHAR(255) NOT NULL 
, CONSTRAINT PSNEXT_PROJECT_TEAM_PK PRIMARY KEY (PROJECT_TEAM_ID )
, CONSTRAINT UQ_Team_Name UNIQUE(PROJECT_TEAM_NAME)
);

CREATE TABLE psnextrequest_db.PSNEXT_PROJECT 
(
  PROJECT_ID BIGINT(20) NOT NULL AUTO_INCREMENT
, PROJECT_PROJECT_TEAM_ID BIGINT(20) NOT NULL
, PROJECT_NAME NVARCHAR(255) NOT NULL 
, PROJECT_STATUS INT(1) NOT NULL DEFAULT '0'
, PROJECT_DESCRIPTION TEXT
, PROJECT_CREATION_DATE DATETIME NOT NULL
, PROJECT_LAST_UPDATE_DATE DATETIME
, CONSTRAINT PSNEXT_PROJECT_PK PRIMARY KEY (PROJECT_ID)
, CONSTRAINT UQ_Name UNIQUE(PROJECT_NAME)
, CONSTRAINT UQ_ProjectTeam_Id UNIQUE(PROJECT_PROJECT_TEAM_ID)
, CONSTRAINT FK_ProjectPerProjectTeam FOREIGN KEY (PROJECT_PROJECT_TEAM_ID) REFERENCES PSNEXT_PROJECT_TEAM (PROJECT_TEAM_ID)
);

CREATE TABLE psnextrequest_db.PSNEXT_USER 
(
  USER_ID BIGINT(20) NOT NULL AUTO_INCREMENT
, USER_FIRST_NAME VARCHAR(48) NOT NULL 
, USER_LAST_NAME VARCHAR(48) NOT NULL 
, USER_DISTINGUISHED_NAME VARCHAR(20) NOT NULL 
, USER_PASSWORD CHAR(128) NOT NULL 
, USER_MAIL VARCHAR(120) NOT NULL 
, CONSTRAINT PSNEXT_USER_PK PRIMARY KEY (USER_ID)
, CONSTRAINT UQ_FirstName_LastName UNIQUE(USER_FIRST_NAME, USER_LAST_NAME)
, CONSTRAINT UQ_DistinguisheName UNIQUE(USER_DISTINGUISHED_NAME)
, CONSTRAINT UQ_UserMail UNIQUE(USER_MAIL)
);

CREATE TABLE psnextrequest_db.PSNEXT_PROJECT_TEAM_USER
(
   USER_ID_psnext_user BIGINT(20) NOT NULL 
,  PROJECT_TEAM_ID_psnext_project_team BIGINT(20) NOT NULL
, CONSTRAINT PSNEXT_PROJECT_TEAM_USER_PK PRIMARY KEY (USER_ID_psnext_user, PROJECT_TEAM_ID_psnext_project_team)
, CONSTRAINT FK_ProjectTeamUserPerUser FOREIGN KEY (USER_ID_psnext_user) REFERENCES PSNEXT_USER (USER_ID)
, CONSTRAINT FK_ProjectTeamUserPerProjectTeam FOREIGN KEY (PROJECT_TEAM_ID_psnext_project_team) REFERENCES PSNEXT_PROJECT_TEAM (PROJECT_TEAM_ID)
);

CREATE TABLE psnextrequest_db.PSNEXT_TASK 
(
  TASK_ID BIGINT(20) NOT NULL AUTO_INCREMENT
, TASK_PROJECT_ID BIGINT(20) NOT NULL 
, TASK_NAME NVARCHAR(255) NOT NULL 
, TASK_STATUS INT(1) NOT NULL DEFAULT '0'
, TASK_DESCRIPTION TEXT
, TASK_CREATION_DATE DATETIME NOT NULL
, TASK_LAST_UPDATE_DATE DATETIME
, CONSTRAINT PSNEXT_TASK_PK PRIMARY KEY (TASK_ID )
, CONSTRAINT UQ_Name UNIQUE(TASK_PROJECT_ID, TASK_NAME)
, CONSTRAINT FK_TaskPerProject FOREIGN KEY (TASK_PROJECT_ID) REFERENCES PSNEXT_PROJECT (PROJECT_ID)
);

CREATE TABLE psnextrequest_db.PSNEXT_REQUEST 
(
  REQ_ID BIGINT(20) NOT NULL AUTO_INCREMENT
, REQ_USER_ID BIGINT(20) NOT NULL 
, REQ_TASK_ID BIGINT(20) NOT NULL 
, REQ_START_DATE DATE NOT NULL 
, REQ_END_DATE DATE NOT NULL 
, REQ_DURATION VARCHAR(28) NOT NULL 
, REQ_CREATION_DATE DATETIME NOT NULL 
, REQ_LAST_UPDATE_DATE DATETIME
, REQ_STATUS INT(1) NOT NULL DEFAULT '0'
, REQ_DESCRIPTION TEXT NOT NULL 
, REQ_COMMENT_RESP TEXT 
, REQ_MANAGED_BY VARCHAR(48)
, REQ_VISIBLE_FOR_USER TINYINT(1) DEFAULT '1'
, CONSTRAINT PSNEXT_REQUEST_PK PRIMARY KEY (REQ_ID)
, CONSTRAINT FK_ReqPerUser FOREIGN KEY (REQ_USER_ID) REFERENCES PSNEXT_USER (USER_ID)
, CONSTRAINT FK_ReqPerTask FOREIGN KEY (REQ_TASK_ID) REFERENCES PSNEXT_TASK (TASK_ID)
);

CREATE TABLE psnextrequest_db.PSNEXT_ROLE 
(
  ROLE_ID BIGINT(20) NOT NULL AUTO_INCREMENT
, ROLE_NAME VARCHAR(15) NOT NULL
, CONSTRAINT PSNEXT_ROLE_PK PRIMARY KEY (ROLE_ID)
, CONSTRAINT UQ_RoleName UNIQUE(ROLE_NAME)
);
INSERT INTO psnextrequest_db.PSNEXT_ROLE(ROLE_ID, ROLE_NAME) VALUES(1,'ressource');
INSERT INTO psnextrequest_db.PSNEXT_ROLE(ROLE_ID, ROLE_NAME) VALUES(2,'admin');

CREATE TABLE psnextrequest_db.PSNEXT_USER_ROLE 
(
   USER_ID_psnext_user BIGINT(20) NOT NULL 
,  ROLE_ID_psnext_role BIGINT(20) NOT NULL
, CONSTRAINT PSNEXT_ROLE_PK PRIMARY KEY (USER_ID_psnext_user, ROLE_ID_psnext_role)
, CONSTRAINT FK_UserRolePerUser FOREIGN KEY (USER_ID_psnext_user) REFERENCES PSNEXT_USER (USER_ID)
, CONSTRAINT FK_UserRolePerRole FOREIGN KEY (ROLE_ID_psnext_role) REFERENCES PSNEXT_PROJECT (PROJECT_ID)
);


CREATE TABLE psnextrequest_db.PSNEXT_RESPONSIBLE 
(
  RESP_TASK_ID BIGINT(20) NOT NULL
, RESP_USER_ID BIGINT(20) NOT NULL 
, IS_VISIBLE TINYINT(1) DEFAULT 1
, CONSTRAINT PSNEXT_RESPONSIBLE_PK PRIMARY KEY (RESP_TASK_ID , RESP_USER_ID )
, CONSTRAINT FK_RespPerUser FOREIGN KEY (RESP_USER_ID) REFERENCES PSNEXT_USER (USER_ID)
, CONSTRAINT FK_RespPerTask FOREIGN KEY (RESP_TASK_ID) REFERENCES PSNEXT_TASK (TASK_ID)
);

CREATE TABLE psnextrequest_db.PSNEXT_REQ_VISIBLE_FOR_RESPONSIBLE 
(
  REQ_ID_REQ BIGINT(20) NOT NULL
, USER_ID_USER BIGINT(20) NOT NULL 
, IS_VISIBLE TINYINT(1) DEFAULT 1
, CONSTRAINT PSNEXT_REQ_VISIBLE_FOR_RESPONSIBLE_PK PRIMARY KEY (REQ_ID_REQ , USER_ID_USER )
, CONSTRAINT FK_ReqVisibleForRespPerUser FOREIGN KEY (USER_ID_USER) REFERENCES PSNEXT_USER (USER_ID)
, CONSTRAINT FK_ReqVisibleForRespPerReq FOREIGN KEY (REQ_ID_REQ) REFERENCES PSNEXT_REQUEST (REQ_ID)
);

CREATE TABLE psnextrequest_db.PSNEXT_REQ_HISTORIC 
(
  REQ_HIST_ID BIGINT(20) NOT NULL AUTO_INCREMENT
, REQ_HIST_REQ_ID BIGINT(20) NOT NULL 
, REQ_HIST_UPDATE_DATE DATETIME NOT NULL
, REQ_HIST_STATUS INT(1) NOT NULL 
, REQ_HIST_START_DATE DATE NOT NULL 
, REQ_HIST_END_DATE DATE NOT NULL 
, REQ_HIST_DURATION VARCHAR(6) NOT NULL
, REQ_HIST_DESCRIPTION TEXT NOT NULL  
, REQ_HIST_COMMENT_RESP TEXT 
, REQ_HIST_MANAGED_BY VARCHAR(48)
, REQ_HIST_VISIBLE_FOR_USER TINYINT(1) DEFAULT '1'
, REQ_HIST_VISIBLE_FOR_RESP TINYINT(1) DEFAULT '1' 
, CONSTRAINT PSNEXT_HISTORIC_PK PRIMARY KEY (REQ_HIST_ID)
, CONSTRAINT FK_HistPerReq FOREIGN KEY (REQ_HIST_REQ_ID) REFERENCES PSNEXT_REQUEST (REQ_ID)
);

CREATE TABLE psnextrequest_db.PSNEXT_GROUP
(
  GROUP_ID BIGINT(20) NOT NULL AUTO_INCREMENT
, GROUP_NAME NVARCHAR(58) NOT NULL 
, CONSTRAINT PSNEXT_HISTORIC_PK PRIMARY KEY(GROUP_ID)
, CONSTRAINT UQ_GroupName UNIQUE(GROUP_NAME)
);
CREATE TABLE psnextrequest_db.PSNEXT_USER_GROUP
(
   USER_ID_psnext_user BIGINT(20) NOT NULL 
,  GROUP_ID_psnext_group BIGINT(20) NOT NULL
, CONSTRAINT PSNEXT_USER_GROUP_PK PRIMARY KEY (USER_ID_psnext_user, GROUP_ID_psnext_group)
, CONSTRAINT FK_UserGroupPerUser FOREIGN KEY (USER_ID_psnext_user) REFERENCES PSNEXT_USER (USER_ID)
, CONSTRAINT FK_UserGroupPerGroup FOREIGN KEY (GROUP_ID_psnext_group) REFERENCES PSNEXT_GROUP (GROUP_ID)
);

