import {Component} from '@angular/core';
import {User} from "./user";

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  public user: User;

  constructor() {
    this.user = {
      id: 1,
      name: 'Pippo'
    };
  }

}
