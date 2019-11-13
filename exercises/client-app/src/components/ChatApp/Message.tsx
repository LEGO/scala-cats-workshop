import React, {FC} from 'react'
import './Message.scss'

interface IChatMessage {
    username: string
    timestamp: number
    text: string
}

interface Props {
    data: IChatMessage
}

const time2Date = (time: number) => {
    const date = new Date(time * 1000)
    return date.toLocaleTimeString()
}

export const Message: FC<Props> = ({data}) => {
    return (
        <div className={'message'}>
            <div className={'message-handler'}>
                <span className={'message-handler-user'}>{data.username}</span>
                <span className={'message-handler-date'}>{time2Date(data.timestamp)}</span>
            </div>
            <div className={'message-text'}>
                <span>{data.text}</span>
            </div>
        </div>
    )
}