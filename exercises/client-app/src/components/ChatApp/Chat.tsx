import React, {FC, useEffect, useRef} from 'react'
import {Message} from './Message'
import {Input} from './Input'
import './Chat.scss'

interface Props {
    messages: any[]
}

export const Chat: FC<Props> = ({messages}) => {
    const chatBottom = useRef<any>()

    useEffect(() => {
        chatBottom.current!.scrollIntoView({behavior: 'smooth'})
    }, [messages])

    return (
        <div className='chat'>
            <div className='chat-messages'>
                {messages.map((message: any, key: number) => (
                    <Message key={key} data={message}/>
                ))}
                <div ref={chatBottom}/>
            </div>
            <div className='chat-input'>
                <Input/>
            </div>
        </div>
    )
}