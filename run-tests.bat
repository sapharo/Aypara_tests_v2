@echo off
cd /d C:\Users\User\IdeaProjects\Test457
call mvn clean test
call mvn allure:generate --clean