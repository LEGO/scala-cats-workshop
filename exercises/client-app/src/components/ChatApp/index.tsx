import React, {FC, useContext, useEffect, useState} from 'react'
import {Users} from './Users'
import {Chat} from './Chat'
import {Socket} from '../../Websocket'
import './index.scss'

export const ChatApp: FC = () => {
    const {client, requestedUsername} = useContext(Socket)
    const [username, setUsername] = useState(requestedUsername)
    const [users, userList] = useState([])
    const [messages, addMessage] = useState([])

    useEffect(() => {
        client!.onmessage = (message) => {
            const data = JSON.parse(message.data as string)
            console.log('chat app', data)
            if (data.Connected) {
                setUsername(data.Connected.username)
                userList(data.Connected.userList)
            }
            if (data.UserJoined) {
                userList(data.UserJoined.userList)
            }
            if (data.UserLeft) {
                userList(data.UserLeft.userList)
            }
            if (data.Message) {
                addMessage(messages.concat(data.Message))
            }
        }
    }, [client, messages, username])

    return (
        <div className='chat-app'>
            <Users username={username} users={users}/>
            <Chat messages={messages}/>
        </div>
    )
}
