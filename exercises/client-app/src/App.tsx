import React, {useState} from 'react'
import {Websocket} from './Websocket'
import {CreateUser} from './components/CreateUser'
import {ChatApp} from './components/ChatApp'

const App: React.FC = () => {
    const [user, createUser] = useState('')

    return (
        <>
            {user === '' ? (
                <CreateUser createUser={createUser}/>
            ) : (
                <Websocket username={user}>
                    <ChatApp/>
                </Websocket>
            )}
        </>
    )
}

export default App
