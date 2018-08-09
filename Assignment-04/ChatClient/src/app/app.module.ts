import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';

import {AppComponent} from './app.component';
import {LoginComponent} from './login/login.component';
import {SidebarComponent} from './sidebar/sidebar.component';
import {MessagesComponent} from './messages/messages.component';
import {SendFieldComponent} from './send-field/send-field.component';
import {AppRoutingModule} from './app-routing.module';
import {ChatComponent} from './chat/chat.component';
import {RoomComponent} from "./room/room.component";
import {FormsModule} from "@angular/forms";
import { LoginFormComponent } from './login-form/login-form.component';

@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    SidebarComponent,
    MessagesComponent,
    SendFieldComponent,
    ChatComponent,
    RoomComponent,
    LoginFormComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
