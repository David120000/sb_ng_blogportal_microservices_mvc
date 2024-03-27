import { AuthToken } from "./auth-token";

export class AuthenticatedUser {

    private _subjectId: string | undefined;
    private _securityToken: AuthToken | undefined;
    
    constructor(subjectId: string | undefined, securityToken: AuthToken | undefined) {
        this._subjectId = subjectId;
        this._securityToken = securityToken;
    }


    public get subjectId(): string | undefined {
        return this._subjectId;
    }
    
    public get securityToken(): AuthToken | undefined {
        return this._securityToken;
    }

    public hasToken(): boolean {
        return (this._securityToken?.jwt != undefined);
    }

}
