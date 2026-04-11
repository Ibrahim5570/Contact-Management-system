USE master;
GO

-- Enable SQL Server + Windows Authentication mode
EXEC xp_instance_regwrite N'HKEY_LOCAL_MACHINE',
N'Software\Microsoft\MSSQLServer\MSSQLServer',
N'LoginMode', REG_DWORD, 2;
GO

-- Create the login
CREATE LOGIN cms_user WITH PASSWORD = 'CmsApp@123';
GO

USE ContactManagementDB;
GO

CREATE USER cms_user FOR LOGIN cms_user;
GO

ALTER ROLE db_owner ADD MEMBER cms_user;
GO