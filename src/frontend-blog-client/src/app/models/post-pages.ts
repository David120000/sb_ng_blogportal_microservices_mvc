import { Post } from "./post";

export class PostPages {

    totalPages: number | undefined;
    totalElements: number | undefined;
    size: number | undefined;
    number: number | undefined;
    numberOfElements: number | undefined;
    first: boolean | undefined;
    last: boolean | undefined;
    empty: boolean | undefined;

    content: Array<Post> | undefined;

}
