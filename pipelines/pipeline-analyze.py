#!/usr/bin/python3

import argparse

import pipelinemodule

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        prog='./pipeline-analyze.py',
        description='Script to analyze all artifacts',
        epilog="Analyzing with sonarqube will be ignore if '-r' is missing.")

    parser.add_argument('-r', '--remote', action='store_true',
                        help="Analyses the code with a remote sonarqube.")
    args = parser.parse_args()

    tasks = [
        ["jacocoTestReport"],
    ]
    if args.remote:
        tasks.append(["sonar"])

    pipelinemodule.execute_gradle_tasks("Analyze all libraries", tasks)
