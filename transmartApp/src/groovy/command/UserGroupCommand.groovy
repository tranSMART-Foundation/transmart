package command

import grails.validation.Validateable

/**
 * @author JIsikoff
 */
@Validateable
class UserGroupCommand {
    String[] userstoadd
    String[] userstoremove
    String[] groupstoadd
    String[] groupstoremove
    String searchtext
}
