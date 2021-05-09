package ringui

import react.RBuilder
import react.RClass
import react.RHandler
import react.RProps

@JsModule("@jetbrains/ring-ui/components/user-card/user-card")
private external object UserCardModule {
    val UserCard: RClass<UserCardProps>
}

// https://github.com/JetBrains/ring-ui/blob/master/components/user-card/card.js
public external interface UserCardProps : RProps {
    public var user: UserCardModel
    public var wording: UserCardWording
}

public data class UserCardModel(
    val name: String,
    val login: String,
    val avatarUrl: String,
    val email: String? = null,
    val href: String? = null
)

public data class UserCardWording(
    val banned: String,
    val online: String,
    val offline: String
)

public fun RBuilder.ringUserCard(user: UserCardModel, handler: RHandler<UserCardProps> = {}) {
    UserCardModule.UserCard {
        attrs.user = user
        handler()
    }
}