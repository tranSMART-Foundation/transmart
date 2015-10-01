package heim
import smartR.plugin.SmartRService


def test_getScriptsFolder(){
    def smartrService = new SmartRService()
    assert smartrService.getScriptsFolder() == '/web-app/HeimScripts'
    println("getScriptsFolder test PASSED")
}

test_getScriptsFolder()