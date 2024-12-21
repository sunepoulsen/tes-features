#!/usr/bin/python3

import pipelinemodule

if __name__ == '__main__':
    pipelinemodule.execute_gradle_tasks("Build libraries", [
        ["build"],
        ["JavaDoc"]
    ])
