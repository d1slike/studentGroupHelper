#!/bin/bash

nohup java -Xmx512m -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5005 -jar student_helper-1.0.0-SNAPSHOT.jar > stdout.log 2>1&