#!/usr/bin/python3

import pipelinemodule

if __name__ == '__main__':
    pipelinemodule.execute_gradle_tasks("Run Component Tests", [
        [":tes-features-component-tests:test", "-Pcomponent-tests"],
    ])
