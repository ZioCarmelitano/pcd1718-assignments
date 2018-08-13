import {Component, OnInit} from '@angular/core';
import { Router } from '@angular/router';
import {ChatUser} from '../chat-user';
import {ChatService} from '../chat.service';

@Component({
  selector: 'app-login-form',
  templateUrl: './login-form.component.html',
  styleUrls: ['./login-form.component.css']
})
export class LoginFormComponent implements OnInit {

  user: ChatUser;

  constructor(private service: ChatService, private router: Router) {
  }

  ngOnInit() {
    this.user = new ChatUser('...');
    const newUserSub = this.service.onNewUser().subscribe(newUser => {
      this.service.user.id = newUser.id;
      newUserSub.unsubscribe();
      this.router.navigate(['/chat']);
    });
  }

  login() {
    this.service.sendNewUser(this.user);
    this.service.user.name = this.user.name;
  }
}
