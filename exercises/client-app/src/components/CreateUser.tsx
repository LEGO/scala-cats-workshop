import React, {FC, useState} from 'react'
import './CreateUser.scss'

interface Props {
    createUser: (userName: string) => void
}

export const CreateUser: FC<Props> = ({createUser}) => {
    const [userName, setUserName] = useState('')

    const submitUser = (e: any) => {
        e.preventDefault()
        createUser(userName)
        setUserName('')
    }

    return (
        <div className='modal-overlay'>
            <div className='modal-wrapper'>
                <h2>Insert your username</h2>
                <form onSubmit={submitUser}>
                    <input
                        type={'text'}
                        placeholder={'your username'}
                        value={userName}
                        minLength={1}
                        required={true}
                        autoFocus={true}
                        onChange={e => {
                            e.preventDefault()
                            setUserName(e.target.value)
                        }}
                    />
                </form>
            </div>
        </div>
    )
}