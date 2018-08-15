//#Patterns: separate-by-one-line-in-contract
contract MultiOwnable { 
    address[] public owners;


    event OwnerAdded(address indexed newOwner);


    /**
     * @dev The Ownable constructor sets the original `owner` of the contract to the sender
     * account.
     */
    //#Err: separate-by-one-line-in-contract
    function MultiOwnable() public {
        owners.push(msg.sender);
    }


    /**
     * @dev Throws if called by any account other than the owner.
     */
     //#Err: separate-by-one-line-in-contract
    modifier onlyOwner() {
        require(isOwner(msg.sender));
        _;
    }
}
