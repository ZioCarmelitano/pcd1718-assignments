import {User} from "./user";

export class ChatUser implements User{
  constructor(
    public name: string,
    public id?: number
  ) {}
}
