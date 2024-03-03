import { NewPostDTO } from './new-post-dto';

describe('NewPostDTO', () => {

  let authorEmail = "mytestmail@host.com";
  let content = "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec sagittis risus in pulvinar aliquet.";
  let published = false;

  it('should create an instance', () => {
    expect(new NewPostDTO(authorEmail, content, published)).toBeTruthy();
  });
});
