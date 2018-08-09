import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {IndexComponent} from './index/index.component';
import {LoginComponent} from './login/login.component';
import {SidebarComponent} from './sidebar/sidebar.component';
import {MessagesComponent} from './messages/messages.component';
import {SendFieldComponent} from './send-field/send-field.component';
import {RoomComponent} from './room/room.component';
import {EventBusService} from "./event-bus.service";
import {ChatService} from "./chat.service";

@NgModule({
  declarations: [
    AppComponent,
    IndexComponent,
    LoginComponent,
    SidebarComponent,
    MessagesComponent,
    SendFieldComponent,
    RoomComponent
  ],
  imports: [
    BrowserModule
  ],
  providers: [
    EventBusService,
    ChatService
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}
