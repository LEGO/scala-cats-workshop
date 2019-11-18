import React, {FC} from 'react'

import './Message.scss'

interface IChatMessage {
    username: string
    timestamp: number
    text: string
    isEmote: boolean
}

interface Props {
    data: IChatMessage
}

const time2Date = (time: number) => {
    const date = new Date(time * 1000)
    return date.toLocaleTimeString()
}

export const Message: FC<Props> = ({data}) => {
    console.log(data)
    if (data.isEmote) {
        const output = data.text + ' <span class=\'message-handler-date\'>' + time2Date(data.timestamp) + '</span>'
        return (
            <div className={'message'}>
                <div className={'message-handler'}>
                    <pre className={'message-text'} dangerouslySetInnerHTML={{__html: output}}/>
                </div>
            </div>
        )
    }
    return (
        <div className={'message'}>
            <div className={'message-handler'}>
                <span className={'message-handler-user'}>{data.username}</span>
                <span className={'message-handler-date'}>{time2Date(data.timestamp)}</span>
            </div>
            <pre className={'message-text'} dangerouslySetInnerHTML={{__html: data.text}}/>
        </div>
    )
}