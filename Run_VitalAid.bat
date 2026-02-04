@echo off
title VitalAid Management System

REM ===========================================
REM Paths
REM ===========================================

REM Path to JavaFX 21 SDK lib folder
set FX_LIB="C:\Users\Vinay Reddy\Downloads\javafx-sdk-21\javafx-sdk-21.0.9\lib"

REM Path to your fat JAR
set JAR_FILE="C:\Users\Vinay Reddy\OneDrive\Documents\NetBeansProjects\Vital_aid\target\VitalAid-1.0.0-jar-with-dependencies.jar"

REM Launch the application
java --module-path %FX_LIB% --add-modules javafx.controls,javafx.fxml -jar %JAR_FILE%

REM Keep the window open after app closes
echo.
echo ===========================================
echo Application has exited. Press any key to close.
pause

