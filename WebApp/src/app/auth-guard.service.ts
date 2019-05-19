import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {WebService} from "./web.service";

@Injectable({
  providedIn:'root'
})
export class AuthGuardService implements CanActivate {

  debugMode = true;

  constructor(private router: Router, private ws: WebService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.debugMode == true) return true;
    if (this.ws.isLoggedIn()) {
      return true;
    } else {
      this.router.navigate(['login']);
      return false;
    }
  }

}
