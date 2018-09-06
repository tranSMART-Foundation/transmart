### tranSMART Spring Security Auth0 Plugin

Note that this is *_not_* a general-purpose plugin integrating Auth0, rather it is an implementation specific to the needs of HMS tranSMART installations.
It is usable in other environments as long as the feature set is sufficient, and of course more Auth0 features and custom logic can be added as needed.

##### Config settings

The plugin has several config options, most with the `grails.plugin.springsecurity.auth0` prefix (abbreviated to `g.p.s.a` in the table below).

For more details, see the `org.transmart.plugin.auth0.Auth0Config` Spring bean class which collects all config values used by the plugin (except `active`, which is evaluated before Spring beans are initialized).

Name | Meaning | Default
---- | ------- | -------
`g.p.s.a.active` | Set to `true` to enable the plugin | `false`
`g.p.s.a.clientId` | The Auth0 client ID | none, must be specified
`g.p.s.a.clientSecret` | The Auth0 client secret | none, must be specified
`g.p.s.a.domain` | The Auth0 domain | none, must be specified
`g.p.s.a.loginCallback` | The application URI for Auth0 to call after a successful authentication | `'/login/callback'`
`g.p.s.a.registrationEnabled` | Set to `true` to enable user self-registration, or `false` to only allow admins to create users | `true`
`g.p.s.a.webtaskBaseUrl` | The base URL for Webtask calls | none
`g.p.s.a.admin.autoCreate` | Set to `true` to automatically create a user with `ROLE_ADMIN` at startup if there are no users with `ROLE_ADMIN` | `false`
`g.p.s.a.admin.autoCreateEmail` | Optional e-mail value to use when auto-creating an admin user | none, should be specified
`g.p.s.a.admin.autoCreatePassword` | The password value to use when auto-creating an admin user (can be cleartext or pre-hashed with bcrypt) | none, must be specified if `autoCreate` is `true`
`g.p.s.a.admin.autoCreateUsername` | The username value to use when auto-creating an admin user | none, must be specified if `autoCreate` is `true`
`edu.harvard.transmart.auth0.emailMessage.level1` | Level-specific message to include in the user email for Level 1 users | none, should be specified
`edu.harvard.transmart.auth0.emailMessage.level2` | Level-specific message to include in the user email for Level 2 users | none, should be specified
`edu.harvard.transmart.auth0.emailMessage.admin` | Level-specific message to include in the user email for Admin users | none, should be specified
`edu.harvard.transmart.email.notify` | Admin e-mail address for notification emails | none, must be specified

Your configuration values should look like this example:

```
grails {
   plugin {
      springsecurity {
         auth0 {
            active = true
            admin {
               autoCreate = false
               autoCreateEmail = 'foo@bar.com'
               // autoCreatePassword = '$2a$14$5z3fZdvO4E3czklRIp4JOuxH9Z2tSm52BfqwEzsszXnaZxhAs.QYy'
               autoCreatePassword = 'password'
               autoCreateUsername = 'adman'
            }
            clientId = '...'
            clientSecret = '...'
            domain = '...'
            registrationEnabled = true
            webtaskBaseUrl = '...'
         }
         ...
      }
   }
}

edu.harvard.transmart.auth0.emailMessage.level1 = '<b>Level 1</b> access allows you ...'
edu.harvard.transmart.auth0.emailMessage.level2 = '<b>Level 2</b> access will allow you ...'
edu.harvard.transmart.auth0.emailMessage.admin = '<b>Admin</b> access will allow you ...'
edu.harvard.transmart.email.notify = '...'
```

or non-nested if you prefer:

```
grails.plugin.springsecurity.auth0.active = true
grails.plugin.springsecurity.auth0.admin.autoCreate = false
...
edu.harvard.transmart.email.notify = '...'
...
```

##### Webtask JavaScript and CSS

When the plugin is enabled, the login page uses installation-specific JavaScript and CSS to support different authentication providers (e.g. Google OAuth, ORCID, etc.) in each environment.

The JavaScript is retrieved via a call to `WEBTASK_BASE_URL/connection_details_base64?webtask_no_cache=1&client_id=AUTH0_CLIENT_ID` and the CSS via `WEBTASK_BASE_URL/connection_details_base64?webtask_no_cache=1&css=true`, where `WEBTASK_BASE_URL` is the config value for `grails.plugin.springsecurity.auth0.webtaskBaseUrl` and `AUTH0_CLIENT_ID` is the config value for `grails.plugin.springsecurity.auth0.clientId`, e.g. `https://some_name.us.webtask.io/connection_details_base64?webtask_no_cache=1&client_id=abc123` and `https://some_name.us.webtask.io/connection_details_base64?webtask_no_cache=1&css=true`,

Each call is made once and cached using the Grails `cache` plugin.

Note that the use of Webtask is not required; any endpoint that returns valid JavaScript and CSS based on the querystring parameters will work.

##### e-mail

The plugin sends emails, and delegates the sending to the `mail` plugin.

If user self-registration is enabled, an admin email is sent to the address specified by the `edu.harvard.transmart.email.notify` config option.

Additionally, whether user self-registration is enabled or users are created in the admin UI, an email is sent to the user notifying of initial account creation and subsequent user level changes (if the user account has a non-null email address).

##### URL Mappings

The plugin overrides some URL mappings from the spring-security-core plugin (`/login/auth`, `/login/authfail`, and `/logout`) to implement custom logic.

Additionally it overrides some `AuthUserController` actions via URL mappings, e.g. `/authUser/create`, `/authUser/list`, etc. to add custom Auth0-specific logic when viewing, creating, updating and deleting users in the admin UI.

##### Build Status

[![Snapshot Build Status](http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/jenkins/view/New%20Plugins/job/spring-security-auth0-snapshots/badge/icon)](http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/jenkins/view/New%20Plugins/job/spring-security-auth0-snapshots/)
[![Release Build Status](http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/jenkins/view/New%20Plugins/job/spring-security-auth0-releases/badge/icon)](http://ec2-35-170-59-132.compute-1.amazonaws.com:8080/jenkins/view/New%20Plugins/job/spring-security-auth0-releases/)
