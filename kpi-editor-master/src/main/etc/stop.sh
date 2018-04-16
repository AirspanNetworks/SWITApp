#!/bin/bash
# Grabs and kill a process from the pidlist that has the word kpi-editor

pid=`ps aux | grep kpi-editor | awk '{print $2}'`
kill -9 $pid
