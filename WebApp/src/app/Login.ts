export class Login {

  password: string;
  userName: string;

  constructor(userName: string, password: string) {
    this.password = password;
    this.userName = userName;
  }
}
