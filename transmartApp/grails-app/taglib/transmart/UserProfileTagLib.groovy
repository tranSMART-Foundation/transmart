package transmart

class UserProfileTagLib {

	def userProfile = { attrs ->
		out << '<li><a href="' + createLink(controller: 'userProfile') + '">User profile</a></li>'
	}
}
