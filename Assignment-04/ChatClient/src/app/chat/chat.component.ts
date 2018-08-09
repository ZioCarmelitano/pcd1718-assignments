import {Component, Input, OnInit} from '@angular/core';
import {User} from "../user";

@Component({
  selector: 'app-chat',
  templateUrl: './chat.component.html',
  styleUrls: ['./chat.component.css']
})
export class ChatComponent implements OnInit {

  @Input()
  public user: User;

  constructor() {
    this.user =
      {
        id: 0,
        name: 'marco'
      };
  }

  ngOnInit() {
  }

}
