import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { IotdeviceComponent } from './iotdevice/iotdevice.component';
import { SavingsComponent } from './savings/savings.component';

@NgModule({
  declarations: [
    AppComponent,
    IotdeviceComponent,
    SavingsComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
