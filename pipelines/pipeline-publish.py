#!/usr/bin/python3

import argparse

import pipelinemodule

if __name__ == '__main__':
    parser = argparse.ArgumentParser(
        prog='./pipeline-publish.py',
        description='Publish artifacts to local or remote repository')

    parser.add_argument('-r', '--remote', action='store_true',
                        help="All artifacts will be published to the remote repository")

    args = parser.parse_args()

    tasks = []
    if args.remote:
        tasks = [
            [":tes-features-model:publish"]
        ]
    else:
        tasks = [
            [":tes-features-model:publishToMavenLocal"]
        ]

    pipelinemodule.execute_gradle_tasks("Publish artifacts", tasks)
