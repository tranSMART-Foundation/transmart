package galaxy.export.plugin

import grails.transaction.Transactional

import groovy.util.logging.Slf4j

@Slf4j('logger')
@Transactional
class GalaxyUserDetailsService {

        def saveNewGalaxyUser(String username, String galaxyKey, String mailAddress) {
            try{
                def galaxyUser = new GalaxyUserDetails()
                galaxyUser.username = username
                galaxyUser.galaxyKey = galaxyKey
                galaxyUser.mailAddress = mailAddress
                galaxyUser.save()
            }
            catch(e){
                logger.error("The export job for galaxy couldn't be saved")
                return false
            }
            return true
        }

    def deleteUser(def username){
        def galaxyUser = GalaxyUserDetails.findByUsername(username.toString())
        galaxyUser.delete()
    }

}
