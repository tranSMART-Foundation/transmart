package command

import grails.validation.Validateable

/**
 * @author JIsikoff
 */
@Validateable
class SecureObjectAccessCommand {
    String[] sobjectstoadd
    String[] sobjectstoremove
    String[] groupstoadd
    String[] groupstoremove
    String accesslevelid
    String searchtext
}
