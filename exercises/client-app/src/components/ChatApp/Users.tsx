import React, {FC} from 'react'
import './Users.scss'

interface Props {
    username: string
    users: string[]
}

export const Users: FC<Props> = ({username, users}) => {

    return (
        <div className='users'>
            <div>@{username}</div>
            <h3>users</h3>
            <div>
                {users.map((user, key) => (<p key={key}>{user}</p>))}
            </div>
        </div>
    )
}