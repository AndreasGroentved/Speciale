import {Injectable} from '@angular/core';


@Injectable({
  providedIn: 'root'
})
export class LoginServiceService {
  private myLocalStorage = localStorage;

  constructor() {
    this.initUser();
  }

  token = "";

  private initUser(): void {
    if (this.myLocalStorage.getItem('token')) {
      try {
        this.token = this.myLocalStorage.getItem('token');
        console.log("token " + this.token);
      } catch (e) {
        console.log('Exception: ' + e);
      }
    }
  }

  public setLogin(token: string) {
    console.log("set login " + token);
    this.myLocalStorage.setItem('token', token);
    this.token = token;
  }
}
