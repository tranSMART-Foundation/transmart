package org.transmartproject.core.users

import org.transmartproject.core.exceptions.NoSuchResourceException

/**
 * Resource related with users.
 */
interface UsersResource {

    User getUserFromUsername(String username) throws NoSuchResourceException
}
