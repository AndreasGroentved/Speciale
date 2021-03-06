import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {IotdevicesComponent} from './iotdevices/iotdevices.component';
import {SavingsComponent} from './savings/savings.component';
import {PowerComponent} from './power/power.component';
import {IotDeviceModuleComponent} from './iotdevice-module/iot-device-module.component';
import {RouterModule, Routes} from '@angular/router';
import {IotDeviceComponent} from './iot-device/iot-device.component';
import {HouseOverviewComponent} from './house-overview/house-overview.component';
import {PageNotFoundComponent} from './page-not-found/page-not-found.component';
import {HttpClientModule} from '@angular/common/http';
import {RuleComponent} from './rule/rule.component';
import {TangleDevicesComponent} from './tangle-devices/tangle-devices.component';
import {ProcurationComponent} from './procuration/procuration.component';
import {NgbModule} from '@ng-bootstrap/ng-bootstrap';
import {RequestDeviceComponent} from './request-device/request-device.component';
import {StrangerDeviceComponent} from './stranger-device/stranger-device.component';
import {NavbarComponent} from './navbar/navbar.component';
import {MatButtonModule, MatSnackBarModule} from '@angular/material'
import {BrowserAnimationsModule} from "@angular/platform-browser/animations";
import {LoginComponent} from './login/login.component';
import {RegisterComponent} from './register/register.component';
import {AuthGuardService} from "./auth-guard.service";

const appRoutes: Routes = [{
  path:'device/:id', component:IotDeviceComponent, canActivate:[AuthGuardService]
}, {path:'tangle', component:TangleDevicesComponent, canActivate:[AuthGuardService]}, {
  path:'stranger', component:StrangerDeviceComponent, canActivate:[AuthGuardService]
}, {path:'house_overview', component:HouseOverviewComponent, canActivate:[AuthGuardService]}, {
  path:'', redirectTo:'/house_overview', pathMatch:'full'
}, {path:'login', component:LoginComponent}, {
  path:'tangle/:id', component:RequestDeviceComponent, canActivate:[AuthGuardService]
}, {
  path:'register', component:RegisterComponent, canActivate:[AuthGuardService]
}, {path:'**', component:PageNotFoundComponent}];

@NgModule({
  declarations:[AppComponent, IotdevicesComponent, SavingsComponent, PowerComponent, IotDeviceModuleComponent, IotDeviceComponent, HouseOverviewComponent, PageNotFoundComponent, ProcurationComponent, RuleComponent, TangleDevicesComponent, RequestDeviceComponent, StrangerDeviceComponent, NavbarComponent, LoginComponent, RegisterComponent],
  imports:[NgbModule, BrowserModule, HttpClientModule, FormsModule, MatButtonModule, MatSnackBarModule, BrowserAnimationsModule, RouterModule.forRoot(appRoutes, {enableTracing:false} // <-- debugging purposes only
  )],
  exports:[NgbModule, BrowserModule, HttpClientModule, FormsModule],

  providers:[AppRoutingModule],
  bootstrap:[AppComponent]
})
export class AppModule {
}
