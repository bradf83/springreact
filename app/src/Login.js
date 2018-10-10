import React, { Component } from 'react';
import { Button, ButtonGroup, Container, Table } from 'reactstrap';
import AppNavbar from './AppNavbar';
import { Link } from 'react-router-dom';

class Login extends Component {

    constructor(props) {
        super(props);
        this.state = {apiKey: '', isLoading: true};
    }

    componentDidMount() {
        this.setState({isLoading: true});

        // {
        //     "usernameOrEmail": "bradf.83@gmail.com",
        //     "password": "secret"
        // }

        // Fetch the token and display it in the console
        // Once we have the token in local storage use it to ask for a protected resource

        // Working - Authenticates

        // fetch('http://localhost:5000/api/auth/signin',{
        //     method: 'post',
        //     headers: {
        //         'Accept': 'application/json, text/plain, */*',
        //         'Content-Type': 'application/json'
        //     },
        //     body: JSON.stringify({usernameOrEmail: 'bradf.83@gmail.com', password: 'secret'})
        // })
        //     .then(response => response.json())
        //     .then(data => console.log(data));

        // Working - Authenticates and then uses token to call a protected resource

        fetch('api/auth/signin',{
            method: 'post',
            headers: {
                'Accept': 'application/json, text/plain, */*',
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({usernameOrEmail: 'bradf.83@gmail.com', password: 'secret'})
        })
            .then(response => response.json())
            .then(data => fetch('api/protected',{
                method: 'post',
                headers: {
                    'Accept': 'application/json, text/plain, */*',
                    'Content-Type': 'application/json',
                    'Authorization': 'Bearer ' + data.accessToken
                },
                body: JSON.stringify({message: 'bradf.83@gmail.com'})
            })
                .then(response => response.json())
                .then(data => console.log(data)));

        // Working
        // fetch('http://localhost:5000/api/protected',{
        //     method: 'post',
        //     headers: {
        //         'Accept': 'application/json, text/plain, */*',
        //         'Content-Type': 'application/json'
        //     },
        //     body: JSON.stringify({message: 'bradf.83@gmail.com'})
        // })
        //     .then(response => response.json())
        //     .then(data => console.log(data));

    }

    render() {
        return (
            <div>
                <AppNavbar/>
                <div>Hellos</div>
            </div>
        );
    }
}

export default Login;