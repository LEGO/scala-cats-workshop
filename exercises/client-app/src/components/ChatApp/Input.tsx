import React, {FC, useContext, useState} from 'react'
import {Socket} from '../../Websocket'

export const Input: FC = () => {
    const {client} = useContext(Socket)
    const [message, setMessage] = useState('')

    const sendMessage = (e: any) => {
        e.preventDefault()
        client!.send(JSON.stringify({text: message}))
        setMessage('')
    }

    return (
        <form onSubmit={sendMessage}>
            <input
                type={'text'}
                placeholder={'write to chat..'}
                value={message}
                minLength={1}
                required={true}
                autoFocus={true}
                onChange={e => {
                    e.preventDefault()
                    setMessage(e.target.value)
                }}/>
        </form>
    )
}