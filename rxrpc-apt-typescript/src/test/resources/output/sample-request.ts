import { SampleData } from './index';
import { StringKeyMap } from 'rxrpc-js';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleRequest
 */
export class SampleRequest {
    data: SampleData;
    mapData: StringKeyMap<any>;
    multipleData: SampleData[];
    id: number;
    name: string;
}
