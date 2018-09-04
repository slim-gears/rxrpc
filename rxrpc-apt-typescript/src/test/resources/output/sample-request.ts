import { SampleData } from './index';
import { StringKeyMap } from 'rxrpc-types';

/**
 * Generated from com.slimgears.rxrpc.sample.SampleRequest
 */
export interface SampleRequest {
    id: number;
    name: string;
    data: SampleData;
    mapData: StringKeyMap<any>;
    multipleData: SampleData[];
}
