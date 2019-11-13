import React, {FC, ReactNode} from 'react'
import {w3cwebsocket as WebSocket, w3cwebsocket} from 'websocket'

interface State {
    username: string
    client: w3cwebsocket | null
}

const initialState: State = {
    username: '',
    client: null,
}

export const Socket = React.createContext(initialState)

interface Props {
    username: string
    children: ReactNode
}

export const Websocket: FC<Props> = ({username, children}) => {
    initialState.client = new WebSocket(`ws://127.0.0.1:4000/chat?username=${username}`)
    initialState.username = username

    return (
        <Socket.Provider value={initialState}>
            {children}
        </Socket.Provider>
    )
}