export class NewPostDTO {

    authorEmail: string;
    content: string;
    published: boolean;

    constructor(authorEmail: string, content: string, published: boolean) {
        this.authorEmail = authorEmail;
        this.content = content;
        this.published = published;
    }

}
