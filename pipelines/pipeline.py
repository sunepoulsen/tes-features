#!/usr/bin/python3

import argparse

import pipelinemodule

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        prog='./pipeline.py',
        description='Pipeline to build, test and publish all artifacts',
        epilog="Analyzing with sonarqube will be ignore if '-r' is missing.")

    parser.add_argument('-r', '--remote', action='store_true',
                        help="Use remote Nexus repository and remote SonarQube")

    args = parser.parse_args()

    scripts = [
        ["./pipeline-clean.py"],
        ["./pipeline-build.py"],
        ["./pipeline-publish.py"],
        ["./pipeline-analyze.py"],
    ]
    pipelinemodule.extend_script_arguments(scripts, "./pipeline-publish.py", args.remote, ["--remote"])
    pipelinemodule.extend_script_arguments(scripts, "./pipeline-analyze.py", args.remote, ["--remote"])

    pipelinemodule.execute_scripts("Building the pipeline", scripts)
