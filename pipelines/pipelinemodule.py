#!/usr/bin/python3

import pathlib
import subprocess
import sys


def find_pipelines_directory():
    return pathlib.Path(__file__).parent.resolve()


def find_project_root_directory():
    return pathlib.Path(__file__).parent.parent.resolve()


def execute_gradle_tasks(message, tasks):
    print(message)

    for task in tasks:
        cmd = ["./gradlew", "--console=plain"] + task
        if subprocess.run(cmd, cwd=find_project_root_directory()).returncode != 0:
            print(f"Gradle task '{task}' failed and execution has been stopped.")
            sys.exit(-1)


def execute_scripts(message, scripts):
    print(message)

    for script in scripts:
        print("")
        if subprocess.run(script, cwd=find_pipelines_directory()).returncode != 0:
            print(f"The pipeline script '{script}' failed and execution has been stopped.")
            sys.exit(-1)


def extend_script_arguments(scripts, name, condition, extra_args):
    for script in scripts:
        if condition and script[0] == name:
            script.extend(extra_args)
