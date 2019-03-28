import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {FormsModule} from '@angular/forms';

import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {IotdevicesComponent} from './iotdevices/iotdevices.component';
import {SavingsComponent} from './savings/savings.component';
import {PowerComponent} from './power/power.component';
import {PriceComponent} from './price/price.component';
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


const appRoutes: Routes = [
  {path: 'device/:id', component: IotDeviceComponent},
  {path: 'tangle', component: TangleDevicesComponent},
  {path: 'stranger', component: StrangerDeviceComponent},
  {path: 'house_overview', component: HouseOverviewComponent},
  {
    path: '',
    redirectTo: '/house_overview',
    pathMatch: 'full'
  },
  {path: 'tangle/:id', component: RequestDeviceComponent},
  {path: '**', component: PageNotFoundComponent}
];

@NgModule({
  declarations: [
    AppComponent,
    IotdevicesComponent,
    SavingsComponent,
    PowerComponent,
    PriceComponent,
    IotDeviceModuleComponent,
    IotDeviceComponent,
    HouseOverviewComponent,
    PageNotFoundComponent,
    ProcurationComponent,
    RuleComponent,
    TangleDevicesComponent,
    RequestDeviceComponent,
    StrangerDeviceComponent
  ],
  imports: [
    NgbModule,
    BrowserModule,
    HttpClientModule,
    FormsModule,
    RouterModule.forRoot(
      appRoutes,
      {enableTracing: true} // <-- debugging purposes only
    )
  ],
  exports: [
    NgbModule,
    BrowserModule,
    HttpClientModule,
    FormsModule
  ],

  providers: [AppRoutingModule],
  bootstrap: [AppComponent]
})
export class AppModule {
}
